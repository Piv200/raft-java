package com.vato.app;

public class Leader extends State{
    public Leader(Node context){
        super(context);
    }

    public void heartbeatElapsed(){
        context.sendEmptyAppendEntries();
        context.setTimetout(context.heartBeatTimeout, 0);
    }

}
