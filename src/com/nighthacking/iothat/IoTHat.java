package com.nighthacking.iothat;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 * @author Stephen Chin <steveonjava@gmail.com>
 */
public class IoTHat {

  static final byte[] readUID = new byte[]{(byte) 0xFF, (byte) 0xCA, 0, 0, 0};
  static final Properties cardids = new Properties();
  static String lastcard;

  public static void main(String[] args) throws CardException, IOException {
    cardids.load(IoTHat.class.getResourceAsStream("cardid.properties"));

    TerminalFactory factory = TerminalFactory.getDefault();
    CardTerminals terminals = factory.terminals();
    List<CardTerminal> list = terminals.list();
    CardTerminal cardTerminal = list.get(0);

    while (true) {
      cardTerminal.waitForCardPresent(0);
      handleCard(cardTerminal);
      cardTerminal.waitForCardAbsent(0);
    }
  }

  private static void handleCard(CardTerminal cardTerminal) {
    try {
      Card card = cardTerminal.connect("*");
      CardChannel channel = card.getBasicChannel();
      CommandAPDU command = new CommandAPDU(readUID);
      ResponseAPDU response = channel.transmit(command);
      byte[] uidBytes = response.getData();
      final String uid = bytesToString(uidBytes);
      if (cardids.containsKey(uid)) {
        final String cardname = cardids.getProperty(uid);
        if (!cardname.equals(lastcard)) {
          lastcard = cardname;
          System.out.println(cardname);
        }
      } else {
        System.out.println("Unknown card: " + uid);
      }
    } catch (CardException ex) {
      Logger.getLogger(IoTHat.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static String bytesToString(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }
}
