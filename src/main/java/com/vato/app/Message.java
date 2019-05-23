package com.vato.app;

import java.io.IOException;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

/**
 * Abstract base class for all messages used in raft.
 */
public abstract class Message {

    private Messages type;
    private String sender;
    private int term;

    public Message(String sender){
        this(sender,0);
    }

    public Message(String sender, int term){
        this.sender = sender;
        this.term = term;
    }

    public Messages getType() {
        return type;
    }
    public void setType(Messages type) {
        this.type = type;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * This method serialises the key components of a message object using MessagePack.
     * Extend this method to pack additional parts of other messages.
     * @return A serialised byte array of the object, in MessagePack format.
     * @throws IOException
     */
    public byte[] serialise()
            throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packInt(type.ordinal());
        packer.packInt(term);
        packer.packString(sender);
        packer.close();
        return packer.toByteArray();
    }

    /**
     * This mthod deserialises the given byte array that is in serialised MessagePack format.
     * @param packed The packed MessagePack byte array for a message.
     * @throws IOException
     */
    public void deserialise(byte[] packed)
            throws IOException {
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packed);
        this.type = Messages.values()[unpacker.unpackInt()];
        this.term = unpacker.unpackInt();
        this.sender = unpacker.unpackString();
        unpacker.close();
    }

    /**
     * Gets the type of message encoded in the packed array.
     * @param packed A packed message.
     * @return The type of message as a Messages enum.
     * @throws IOException
     */
    public static Messages getMessageType(byte[] packed)
            throws IOException, ArrayIndexOutOfBoundsException{
        //TODO: Try and find a better way to deserialise messages, than basically doing it twice. Could borrow from python design...
        //TODO: Exception handling for array out of bounds.
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packed);
        Messages t = Messages.values()[unpacker.unpackInt()];
        unpacker.close();
        return t;
    }
}
