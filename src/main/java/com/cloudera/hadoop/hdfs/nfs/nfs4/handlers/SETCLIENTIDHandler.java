package com.cloudera.hadoop.hdfs.nfs.nfs4.handlers;

import static com.cloudera.hadoop.hdfs.nfs.nfs4.Constants.*;
import static com.google.common.base.Preconditions.*;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.hadoop.hdfs.nfs.Bytes;
import com.cloudera.hadoop.hdfs.nfs.nfs4.Callback;
import com.cloudera.hadoop.hdfs.nfs.nfs4.Client;
import com.cloudera.hadoop.hdfs.nfs.nfs4.ClientFactory;
import com.cloudera.hadoop.hdfs.nfs.nfs4.ClientID;
import com.cloudera.hadoop.hdfs.nfs.nfs4.NFS4Exception;
import com.cloudera.hadoop.hdfs.nfs.nfs4.NFS4Handler;
import com.cloudera.hadoop.hdfs.nfs.nfs4.OpaqueData8;
import com.cloudera.hadoop.hdfs.nfs.nfs4.Session;
import com.cloudera.hadoop.hdfs.nfs.nfs4.requests.SETCLIENTIDRequest;
import com.cloudera.hadoop.hdfs.nfs.nfs4.responses.SETCLIENTIDResponse;

public class SETCLIENTIDHandler extends OperationRequestHandler<SETCLIENTIDRequest, SETCLIENTIDResponse> {
  protected static final Logger LOGGER = LoggerFactory.getLogger(SETCLIENTIDHandler.class);

  protected static final AtomicLong VERIFER = new AtomicLong(0);
  @Override
  protected SETCLIENTIDResponse doHandle(NFS4Handler server, Session session,
      SETCLIENTIDRequest request) throws NFS4Exception {
    /*
     * TODO should follow RFC 3530 page ~211
     */
    ClientID clientID = checkNotNull(request.getClientID(), "clientid");
    Callback callback = checkNotNull(request.getCallback(), "callback");
    ClientFactory clientFactory = server.getClientFactory();
    Client client = clientFactory.createIfNotExist(clientID);
    if(client == null) {
      client = checkNotNull(clientFactory.get(clientID.getOpaqueID()), "client should exist");
      if(!session.getClientHost().equals(client.getClientHost())) {
        throw new NFS4Exception(NFS4ERR_CLID_INUSE, 
            "Session is '" + session.getClientHost() + "' and client is '" + client.getClientHost() + "'");
      }
      // update callback info below and client verifer here
      client.getClientID().setVerifer(clientID.getVerifer());
    }
    // server verifer
    OpaqueData8 verifer = new OpaqueData8();
    verifer.setData(Bytes.toBytes(VERIFER.addAndGet(10)));
    client.setCallback(callback);
    client.setVerifer(verifer);
    client.setClientHostPort(session.getClientHostPort());
    client.setClientHost(session.getClientHost());
    SETCLIENTIDResponse response = createResponse();
    response.setClientID(client.getShorthandID());
    response.setVerifer(verifer);
    response.setStatus(NFS4_OK);
    return response;
  }

  @Override
  protected SETCLIENTIDResponse createResponse() {
    return new SETCLIENTIDResponse();
  }

}
