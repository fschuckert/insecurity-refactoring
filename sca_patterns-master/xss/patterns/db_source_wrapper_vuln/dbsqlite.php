<?php

require_once 'dbinterface.php';


/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Interface to the improved MySQL extension (MySQLi)
 *
 * @package    PhpMyAdmin-DBI
 * @subpackage MySQLi
 */
class SQLite implements DBInterface
{
    private $pdo;

    public function connect() {

        // NULL enables connection to the default socket

        $this->pdo = new PDO('sqlite:test.sql', null, null, array(PDO::ATTR_PERSISTENT => true));
        $this->pdo->query("CREATE TABLE tables (table_name varchar(255));");

        return $this->pdo;
    }


    public function realQuery($query)
    {        
        $retval = 'retval';

        $stmt = $this->pdo->prepare($query);
        if($stmt->execute(array())){
            while( $row = $stmt->fetch()){
                $retval = $row['table_name'];
            }
            
        }
        
        return $retval;
    }

  
}
?>
