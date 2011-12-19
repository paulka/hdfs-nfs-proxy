package com.cloudera.hadoop.hdfs.nfs.nfs4.handlers;

import static com.cloudera.hadoop.hdfs.nfs.nfs4.Constants.*;

import java.io.IOException;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.hadoop.hdfs.nfs.nfs4.Bitmap;
import com.cloudera.hadoop.hdfs.nfs.nfs4.ChangeInfo;
import com.cloudera.hadoop.hdfs.nfs.nfs4.NFS4Exception;
import com.cloudera.hadoop.hdfs.nfs.nfs4.NFS4Handler;
import com.cloudera.hadoop.hdfs.nfs.nfs4.Session;
import com.cloudera.hadoop.hdfs.nfs.nfs4.attrs.Attribute;
import com.cloudera.hadoop.hdfs.nfs.nfs4.requests.CREATERequest;
import com.cloudera.hadoop.hdfs.nfs.nfs4.responses.CREATEResponse;
import com.google.common.collect.ImmutableMap;



public class CREATEHandler extends OperationRequestHandler<CREATERequest, CREATEResponse> {
  protected static final Logger LOGGER = LoggerFactory.getLogger(CREATEHandler.class);

  @Override
  protected CREATEResponse doHandle(NFS4Handler server, Session session,
      CREATERequest request) throws NFS4Exception, IOException {
    if(session.getCurrentFileHandle() == null) {
      throw new NFS4Exception(NFS4ERR_NOFILEHANDLE);
    }
    if(request.getType() != NFS4_DIR) {
      throw new UnsupportedOperationException("Create files of  type " + request.getType() + " is not supported.");
    }
    if("".equals(request.getName())) {
      throw new NFS4Exception(NFS4ERR_INVAL);
    }
    Path parent = server.getPath(session.getCurrentFileHandle());
    Path path = new Path(parent, request.getName());
    FileSystem fs = session.getFileSystem();
    if(!fs.exists(parent)) {
      throw new NFS4Exception(NFS4ERR_STALE, "Parent " + parent + " does not exist");
    }
    if(fs.exists(path)) {
      throw new NFS4Exception(NFS4ERR_EXIST, "Path " + path + " already exists.");
    }
    long parentModTimeBefore = fs.getFileStatus(parent).getModificationTime();
    if(!fs.mkdirs(path)) {
      throw new NFS4Exception(NFS4ERR_IO);
    }
    long parentModTimeAfter = fs.getFileStatus(parent).getModificationTime();
    FileStatus fileStatus = fs.getFileStatus(path);
    ImmutableMap<Integer, Attribute> requestAttrs = request.getAttrValues();
    // TODO Handlers should have annotations so that setAttrs can throw an
    // error if they require the stateID to be set. 
    Bitmap responseAttrs = Attribute.setAttrs(server, session, 
        request.getAttrs(), requestAttrs, fs, fileStatus, null);
    session.setCurrentFileHandle(server.createFileHandle(path));
    CREATEResponse response = createResponse();
    response.setChangeInfo(ChangeInfo.newChangeInfo(true, parentModTimeBefore, parentModTimeAfter));
    response.setStatus(NFS4_OK);
    response.setAttrs(responseAttrs);
    return response;
  }
    @Override
  protected CREATEResponse createResponse() {
    return new CREATEResponse();
  }
}
