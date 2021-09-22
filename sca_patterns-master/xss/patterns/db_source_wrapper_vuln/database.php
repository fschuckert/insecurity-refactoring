<?php

class Database
{
    private $pdo;

    function __construct()
    {
        $this->pdo = new PDO('sqlite:test.sql', null, null, array(PDO::ATTR_PERSISTENT => true));
        $this->pdo->query("CREATE TABLE tables (table_name varchar(255));");
    }

    function insert($table_name)
    {
        $stmt = $this->pdo->prepare("INSERT INTO tables VALUES (:table_name)");
        $stmt->execute(array(':table_name' => $table_name));
    }
}

?>