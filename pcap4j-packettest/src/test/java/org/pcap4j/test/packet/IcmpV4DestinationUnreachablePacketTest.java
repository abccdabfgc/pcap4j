package org.pcap4j.test.packet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IcmpV4CommonPacket;
import org.pcap4j.packet.IcmpV4DestinationUnreachablePacket;
import org.pcap4j.packet.IcmpV4DestinationUnreachablePacket.IcmpV4DestinationUnreachableHeader;
import org.pcap4j.packet.IcmpV4EchoPacket;
import org.pcap4j.packet.IcmpV4EchoPacket.Builder;
import org.pcap4j.packet.IllegalPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.IllegalRawDataPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV4Rfc1349Tos;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.SimpleBuilder;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IcmpV4Code;
import org.pcap4j.packet.namednumber.IcmpV4Type;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.packet.namednumber.IpVersion;
import org.pcap4j.util.IcmpV4Helper;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("javadoc")
public class IcmpV4DestinationUnreachablePacketTest extends AbstractPacketTest {

  private static final Logger logger =
      LoggerFactory.getLogger(IcmpV4DestinationUnreachablePacketTest.class);

  private final IcmpV4DestinationUnreachablePacket packet;
  private final int unused;

  public IcmpV4DestinationUnreachablePacketTest() {
    this.unused = 12345;

    Builder echob = new Builder();
    echob
        .identifier((short) 100)
        .sequenceNumber((short) 10)
        .payloadBuilder(
            new UnknownPacket.Builder().rawData((new byte[] {(byte) 0, (byte) 1, (byte) 2})));

    IcmpV4CommonPacket.Builder icmpV4b = new IcmpV4CommonPacket.Builder();
    icmpV4b
        .type(IcmpV4Type.ECHO)
        .code(IcmpV4Code.NO_CODE)
        .payloadBuilder(echob)
        .correctChecksumAtBuild(true);

    IpV4Packet.Builder ipv4b = new IpV4Packet.Builder();
    try {
      ipv4b
          .version(IpVersion.IPV4)
          .tos(IpV4Rfc1349Tos.newInstance((byte) 0))
          .identification((short) 100)
          .ttl((byte) 100)
          .protocol(IpNumber.ICMPV4)
          .srcAddr(
              (Inet4Address)
                  InetAddress.getByAddress(new byte[] {(byte) 192, (byte) 0, (byte) 2, (byte) 2}))
          .dstAddr(
              (Inet4Address)
                  InetAddress.getByAddress(new byte[] {(byte) 192, (byte) 0, (byte) 2, (byte) 1}))
          .payloadBuilder(icmpV4b)
          .correctChecksumAtBuild(true)
          .correctLengthAtBuild(true);
    } catch (UnknownHostException e) {
      throw new AssertionError();
    }

    IcmpV4DestinationUnreachablePacket.Builder b = new IcmpV4DestinationUnreachablePacket.Builder();
    b.unused(unused).payload(IcmpV4Helper.makePacketForInvokingPacketField(ipv4b.build()));
    this.packet = b.build();
  }

  @Override
  protected Packet getPacket() {
    return packet;
  }

  @Override
  protected Packet getWholePacket() throws UnknownHostException {
    IcmpV4CommonPacket.Builder icmpV4b = new IcmpV4CommonPacket.Builder();
    icmpV4b
        .type(IcmpV4Type.DESTINATION_UNREACHABLE)
        .code(IcmpV4Code.HOST_UNREACHABLE)
        .payloadBuilder(new SimpleBuilder(packet))
        .correctChecksumAtBuild(true);

    IpV4Packet.Builder ipv4b = new IpV4Packet.Builder();
    ipv4b
        .version(IpVersion.IPV4)
        .tos(IpV4Rfc1349Tos.newInstance((byte) 0))
        .identification((short) 100)
        .ttl((byte) 100)
        .protocol(IpNumber.ICMPV4)
        .srcAddr(
            (Inet4Address)
                InetAddress.getByAddress(new byte[] {(byte) 192, (byte) 0, (byte) 2, (byte) 1}))
        .dstAddr(
            (Inet4Address)
                InetAddress.getByAddress(new byte[] {(byte) 192, (byte) 0, (byte) 2, (byte) 2}))
        .payloadBuilder(icmpV4b)
        .correctChecksumAtBuild(true)
        .correctLengthAtBuild(true);

    EthernetPacket.Builder eb = new EthernetPacket.Builder();
    eb.dstAddr(MacAddress.getByName("fe:00:00:00:00:02"))
        .srcAddr(MacAddress.getByName("fe:00:00:00:00:01"))
        .type(EtherType.IPV4)
        .payloadBuilder(ipv4b)
        .paddingAtBuild(true);
    return eb.build();
  }

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {
    logger.info(
        "########## "
            + IcmpV4DestinationUnreachablePacketTest.class.getSimpleName()
            + " START ##########");
  }

  @AfterAll
  public static void tearDownAfterClass() throws Exception {}

  @Test
  public void testNewPacket() {
    IcmpV4DestinationUnreachablePacket p;
    try {
      p =
          IcmpV4DestinationUnreachablePacket.newPacket(
              packet.getRawData(), 0, packet.getRawData().length);
    } catch (IllegalRawDataException e) {
      throw new AssertionError(e);
    }
    assertEquals(packet, p);

    assertTrue(p.getPayload().contains(IpV4Packet.class));
    assertTrue(p.getPayload().contains(IcmpV4CommonPacket.class));
    assertTrue(p.getPayload().contains(IcmpV4EchoPacket.class));
    assertFalse(p.getPayload().contains(UnknownPacket.class));
    assertFalse(p.getPayload().contains(IllegalPacket.class));
    assertFalse(p.getPayload().contains(IllegalRawDataPacket.class));
  }

  @Test
  public void testGetHeader() {
    IcmpV4DestinationUnreachableHeader h = packet.getHeader();
    assertEquals(unused, h.getUnused());
  }
}
