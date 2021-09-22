<?php



abstract class Database
{
    protected $pdo;

    function __construct()
    {
        $this->pdo = new PDO('sqlite:tests.sql', null, null, array(PDO::ATTR_PERSISTENT => true));
        $this->pdo->query("CREATE TABLE user (id int, val varchar(255));");
    }

    function insert($id, $val)
    {
        $stmt = $this->pdo->prepare("INSERT INTO user VALUES (:id, :val)");
        $stmt->execute(array(':id' => $id, ':val' => $val));
    }


    public abstract function get_records_sql($sql, array $params=null);

    

    public function get_record($sql) {
        return $this->get_record_select($sql, $params);
    }
}

?>