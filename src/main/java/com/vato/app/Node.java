package com.vato.app;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
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

  private UUID id;
  private Vector<OnRaftStateChangedListener> stateChangedListeners;

  /**
   *
   * @param @NotNull messageStrategy
   * @param @NotNull configPath
   * @throws InvalidParameterException
   */
  public Node(MessageStrategy messageStrategy, String configPath)
          throws InvalidParameterException{
    currentTerm = 0;
    log = new Vector<Message>();
    commitIndex = 0;
    lastApplied = 0;
    id = UUID.randomUUID();

    if(messageStrategy == null){
      throw new InvalidParameterException("Message Strategy must not be null");
    }

    this.messageStrategy = messageStrategy;

    // Read JSON config as string.
    // Python version puts this code outside of scope/responsibility of the Node...
    File f = new File(configPath);
    FileInputStream fis;
    try {
       fis = new FileInputStream(f);
       config = new JSONObject(fis);
    }catch(FileNotFoundException e){
      System.out.println("Could not find file " + configPath);
      // TODO: Make a default config.
      System.out.println("Using default configuration");
      return;
    }

    JSONObject raft = config.getJSONObject("raft");
    minTimeout = Integer.parseInt(raft.getString("min_election_timeout"));
    varyTimeout = Integer.parseInt(raft.getString("varying_election_timeout"));
    heartbeatTimeout = minTimeout / 2;
    currentState = new Follower(this);

    stateChangedListeners = new Vector<OnRaftStateChangedListener>();
    timer = new Timer();
  }

  public void addStateChange(OnRaftStateChangedListener changed){
    stateChangedListeners.add(changed);
  }

  public void setState(State newState){
    // This is where garbage collection can be a pain... I need to figure this out better.
    currentState = newState;
    // TODO: figure out java asynchronous calls, as this is naive.
    for(OnRaftStateChangedListener i : stateChangedListeners){
      i.onStateChanged();
    }
  }

  public void timeoutElapsed(){
    currentState.heartbeatElapsed();
  }

  public void setTimeout(){
    if (minTimeout > 0 && varyTimeout > 0){
      setTimeout(minTimeout, varyTimeout);
    }else{
      System.out.println("Could not find a loaded timeout");
    }
  }

  public void setTimeout(int minTimeout, int varyTimeout){
    timer.cancel();
    Random randy = new Random();
    int randNum = randy.nextInt(varyTimeout) + minTimeout;
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        timeoutElapsed();
      }
    }, randNum);
  }

  public void sendRequestVote(){
    messageStrategy.sendRequestVote(new RequestVote(currentTerm, id.toString()));
  }

  public RequestVoteResponse voteRequested(RequestVote voteRequest){
    return currentState.rcvRequestVote(voteRequest);
  }

  public void voteReceived(RequestVoteResponse response){
    currentState.rcvRequestVoteResponse(response);
  }

  public void sendEmptyAppendEntries(){
    messageStrategy.sendAppendEntries(currentTerm, id.toString());
  }

  public void appendEntriesResponseReceived(Response response){
    currentState.rcvAppendEntriesResponse(response);
  }

  public Response rcvAppendEntries(AppendEntries entry){
    if(entry.term < currentTerm){
      return new Response(currentTerm, false);
    }
    return currentState.rcvAppendEntries(entry);
  }
}
