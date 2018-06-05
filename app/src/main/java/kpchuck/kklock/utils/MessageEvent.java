package kpchuck.kklock.utils;

public class MessageEvent {

    public final String color;
    public final int dialogId;

    public MessageEvent(int dialogId, String color) {
        this.color = color;
        this.dialogId = dialogId;
    }
}
