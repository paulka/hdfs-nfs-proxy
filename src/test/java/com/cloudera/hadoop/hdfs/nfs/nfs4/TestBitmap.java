package com.cloudera.hadoop.hdfs.nfs.nfs4;

import static com.cloudera.hadoop.hdfs.nfs.TestUtils.*;
import static com.cloudera.hadoop.hdfs.nfs.nfs4.Constants.*;

import org.junit.Test;

import com.cloudera.hadoop.hdfs.nfs.nfs4.Bitmap;

public class TestBitmap {
  
  @Test
  public void testBitMapLarge() {
    // both first int and second int in bitmap
    Bitmap base = new Bitmap();
    base.set(NFS4_FATTR4_SUPPORTED_ATTRS);
    base.set(NFS4_FATTR4_TYPE);
    base.set(NFS4_FATTR4_CHANGE);
    base.set(NFS4_FATTR4_SIZE);
    base.set(NFS4_FATTR4_FSID);
    base.set(NFS4_FATTR4_FILEID);
    base.set(NFS4_FATTR4_MODE);
    base.set(NFS4_FATTR4_NUMLINKS);
    base.set(NFS4_FATTR4_OWNER);
    base.set(NFS4_FATTR4_OWNER_GROUP);
    base.set(NFS4_FATTR4_RAWDEV);
    base.set(NFS4_FATTR4_SPACE_USED);
    base.set(NFS4_FATTR4_TIME_ACCESS);
    base.set(NFS4_FATTR4_TIME_METADATA);
    base.set(NFS4_FATTR4_TIME_MODIFY);
    base.set(64);
    Bitmap copy = new Bitmap();
    copy(base, copy);
    deepEquals(base, copy);
  }
  
    @Test
    public void testBitMapSmall() {
      // first int only
      Bitmap base = new Bitmap();
      base.set(NFS4_FATTR4_LEASE_TIME);
      base.set(NFS4_FATTR4_MAXFILESIZE);
      base.set(NFS4_FATTR4_MAXREAD);
      base.set(NFS4_FATTR4_MAXWRITE); // 31 (had bug where 31 was not handled)
      base.set(64);
      Bitmap copy = new Bitmap();
      copy(base, copy);
      deepEquals(base, copy);
    }
}
