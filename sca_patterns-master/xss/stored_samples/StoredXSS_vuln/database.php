<?php

class Database
{
    private $pdo;

    function __construct()
    {
        $this->pdo = new PDO('sqlite:test.sql', null, null, array(PDO::ATTR_PERSISTENT => true));
        $this->pdo->query("CREATE TABLE test (id int, val varchar(255));");
    }

    function insert($id, $value)
    {
        $stmt = $this->pdo->prepare("INSERT INTO test VALUES (:id, :val)");
        $stmt->execute(array(':id' => $id, ':val' => $value));
    }

    function getValue($id)
    {
        $retval = "";

        $stmt = $this->pdo->prepare("SELECT val FROM test WHERE id=:id");
        if($stmt->execute(array(':id' => $id))){
            while( $row = $stmt->fetch()){
                $retval = $row['val'];
            }
        }

        return $retval;
    }
}

?>