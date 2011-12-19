package com.cloudera.hadoop.hdfs.nfs.nfs4.attrs;


import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import com.cloudera.hadoop.hdfs.nfs.nfs4.NFS4Exception;
import com.cloudera.hadoop.hdfs.nfs.nfs4.NFS4Handler;
import com.cloudera.hadoop.hdfs.nfs.nfs4.Session;

public class CaseInsensitiveHandler extends AttributeHandler<CaseInsensitive> {

  @Override
  public CaseInsensitive get(NFS4Handler server, Session session,
      FileSystem fs, FileStatus fileStatus) throws NFS4Exception {
    CaseInsensitive support = new CaseInsensitive();
    support.set(false);
    return support;
  }

}
