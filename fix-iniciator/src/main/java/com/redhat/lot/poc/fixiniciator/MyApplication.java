
package com.redhat.lot.poc.fixiniciator;

import javax.inject.Inject;
import javax.swing.SwingUtilities;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

public class MyApplication implements Application {

    public void onCreate(SessionID sessionID) {
    }

    public void onLogon(SessionID sessionID) {
        System.out.println(">>> LOGON: "+sessionID);
    }

    public void onLogout(SessionID sessionID) {
        System.out.println(">>> LOGOUT: "+sessionID);
    }

    public void toAdmin(quickfix.Message message, SessionID sessionID) {
    }

    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
    }

    public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
    }

    public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        try {
            SwingUtilities.invokeLater(new MessageProcessor(message, sessionID));
        } catch (Exception e) {
        }
    }

    public class MessageProcessor implements Runnable {
        private final quickfix.Message message;
        private final SessionID sessionID;

        public MessageProcessor(quickfix.Message message, SessionID sessionID) {
            this.message = message;
            this.sessionID = sessionID;
        }

        public void run() {
            System.out.println("----------------");
            System.out.println("ID: "+sessionID);
            System.out.println("MSG: "+message.toRawString());
        }
    }

    // private void send(quickfix.Message message, SessionID sessionID) {
    //     try {
    //         Session.sendToTarget(message, sessionID);
    //     } catch (SessionNotFound e) {
    //         System.out.println(e);
    //     }
    // }

    // public void send50(Order order) {
    //     quickfix.fix50.NewOrderSingle newOrderSingle = new quickfix.fix50.NewOrderSingle(
    //             new ClOrdID(order.getID()), sideToFIXSide(order.getSide()),
    //             new TransactTime(), typeToFIXType(order.getType()));
    //     newOrderSingle.set(new OrderQty(order.getQuantity()));
    //     newOrderSingle.set(new Symbol(order.getSymbol()));
    //     newOrderSingle.set(new HandlInst('1'));
    //     send(populateOrder(order, newOrderSingle), order.getSessionID());
    // }

}
