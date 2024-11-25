### 类

BlockHeader
Block Hash、Prev Hash、Nonce、List<Transactions>

Block
BlockHeader

### 数据库记录

使用rocksDB：key为height，value为Block对象