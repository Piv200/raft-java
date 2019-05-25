package com.vato.app;

import java.util.Timer;
import java.util.Vector;

import org.json.JSONObject;

public class Node {
    private State currentState;
    private Timer timer;
    private MessageStrategy messageStrategy;

    // Persistent States
    private int currentTerm;
    private String votedFor;
    private Vector<Message> log;

    // Volatile States
    private int commitIndex;
    private int lastApplied;

    private int minTimeout;
    private int varyTimeout;
    private int heartbeatTimeout;
    private JSONObject config;

    private String id;

    public Node(MessageStrategy messageStrategy, String configPath){
        
    }
}
