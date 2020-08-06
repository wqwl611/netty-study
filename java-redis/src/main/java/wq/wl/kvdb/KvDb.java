package wq.wl.kvdb;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Description:
 *
 * @author: wangliang
 * @time: 2020-08-03
 */
@Component
public class KvDb {

    private static final Logger LOG = LoggerFactory.getLogger(KvDb.class);
    private static String charset = "utf8";
    private RocksDB rocksDB;
    @Value("${rocksdb.path}")
    private String path;

    @PostConstruct
    public void init() {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            rocksDB = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            LOG.error("RocksDB open error: ", e);
            System.exit(-1);
        }
    }

    public String get(String key) throws RocksDBException, UnsupportedEncodingException {
        byte[] bytes = rocksDB.get(key.getBytes(charset));
        if (bytes == null) {
            return null;
        }
        return new String(bytes, charset);
    }

    public void set(String key, String value) throws RocksDBException, UnsupportedEncodingException {
        rocksDB.put(key.getBytes(charset), value.getBytes(charset));
    }

    public void del(String key) throws RocksDBException, UnsupportedEncodingException {
        rocksDB.delete(key.getBytes(charset));
    }

    public void delAll() {

    }

}
