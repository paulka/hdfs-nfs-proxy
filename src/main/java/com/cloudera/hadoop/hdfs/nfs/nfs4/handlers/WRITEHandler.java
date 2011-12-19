package com.cloudera.hadoop.hdfs.nfs.nfs4.handlers;

import static com.cloudera.hadoop.hdfs.nfs.nfs4.Constants.*;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.hadoop.hdfs.nfs.Bytes;
import com.cloudera.hadoop.hdfs.nfs.nfs4.FileHandle;
import com.cloudera.hadoop.hdfs.nfs.nfs4.NFS4Exception;
import com.cloudera.hadoop.hdfs.nfs.nfs4.NFS4Handler;
import com.cloudera.hadoop.hdfs.nfs.nfs4.OpaqueData8;
import com.cloudera.hadoop.hdfs.nfs.nfs4.Session;
import com.cloudera.hadoop.hdfs.nfs.nfs4.WriteOrderHandler;
import com.cloudera.hadoop.hdfs.nfs.nfs4.requests.WRITERequest;
import com.cloudera.hadoop.hdfs.nfs.nfs4.responses.WRITEResponse;

public class WRITEHandler extends OperationRequestHandler<WRITERequest, WRITEResponse> {
  protected static final Logger LOGGER = LoggerFactory.getLogger(WRITEHandler.class);

  @Override
  protected WRITEResponse doHandle(NFS4Handler server, Session session,
      WRITERequest request) throws NFS4Exception, IOException {
    if(session.getCurrentFileHandle() == null) {
      throw new NFS4Exception(NFS4ERR_NOFILEHANDLE);
    }
    
    FileHandle fileHandle = session.getCurrentFileHandle();
    Path path = server.getPath(fileHandle);
    String file = path.toUri().getPath();
    FSDataOutputStream out = server.forWrite(request.getStateID(), session.getFileSystem(), fileHandle, false);
    
    LOGGER.info(session.getSessionID() + " Write accepted " + file + " "  + request.getOffset());

    WriteOrderHandler writeOrderHandler = server.getWriteOrderHandler(file, out);
    boolean sync = request.getStable() != NFS4_COMMIT_UNSTABLE4;
    int count = writeOrderHandler.write(session.getXID(), request.getOffset(), 
        sync, request.getData(), request.getStart(), request.getLength());
    
    WRITEResponse response = createResponse();
    OpaqueData8 verifer = new OpaqueData8();
    verifer.setData(Bytes.toBytes(server.getStartTime()));
    response.setVerifer(verifer);
    server.incrementMetric("HDFS_BYTES_WRITE", count);
    response.setCount(count);
    response.setStatus(NFS4_OK);
    return response;
  }

  @Override
  protected WRITEResponse createResponse() {
    return new WRITEResponse();
  }

}
