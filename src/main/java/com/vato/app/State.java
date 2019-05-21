package com.vato.app;

/**
 * The State class is the abstract class for all states that a raft node may be in.
 */
public abstract class State {
    Node context;
    int currentTerm;

    public State(Node context){
        this.context = context;
        currentTerm = 0;
    }

    /**
     * This method is called when the heartbeat timeout for the node has elapsed.
     */
    public abstract void heartbeatElapsed();

    /**
     * This method is called when the node receives a request vote message.
     * @param request The Request Vote message.
     * @return The response for the vote request
     */
    public abstract RequestVoteResponse rcvRequestVote(RequestVote request);

    /**
     * This method is called when a response for a request for vote is received.
     * @param request The response of a previously sent vote request message.
     */
    public abstract void rcvRequestVoteResponse(RequestVoteResponse response);

    /**
     * This method is called when a leader sends an append entries message here.
     * @param request The append entries message.
     * @return A response to the append entrees message.
     */
    public abstract Response rcvAppendEntries(AppendEntries request);

    /**
     * This message is called when a leader is sent an append entries response (maybe put this in leader only?)
     * @param request The append entries response message.
     */
    public abstract void rcvAppendEntriesResponse(Response response);
}
