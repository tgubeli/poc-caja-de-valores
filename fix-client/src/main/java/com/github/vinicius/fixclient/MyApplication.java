
package com.github.vinicius.fixclient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;

import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.BeginString;
import quickfix.field.BusinessRejectReason;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.CxlType;
import quickfix.field.DeliverToCompID;
import quickfix.field.ExecID;
import quickfix.field.HandlInst;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.LocateReqd;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.RefMsgType;
import quickfix.field.RefSeqNum;
import quickfix.field.SenderCompID;
import quickfix.field.SessionRejectReason;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;

public class MyApplication implements Application {

    public void onCreate(SessionID sessionID) {
    }

    public void onLogon(SessionID sessionID) {
        System.out.println("LOGON: "+sessionID);
    }

    public void onLogout(SessionID sessionID) {
        System.out.println("LOGOUT: "+sessionID);
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
