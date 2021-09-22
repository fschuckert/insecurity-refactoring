<?php



class Database
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


    public function get_record($sql) {

        $retval = "";

        $stmt = $this->pdo->prepare($sql);

        if($stmt->execute($params)){
            while( $row = $stmt->fetch()){
                $retval = $row['val'];
            }
        }

        return $retval;
    }
}

?>