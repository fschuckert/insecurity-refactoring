<?php

require_once('database.php');

class DatabaseImpl extends Database
{
    public function get_records_sql($sql, array $params=null)
    {
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