<?php



function query($sql)
{
    $db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
    // error_log("db $db");
    return $db->query($sql);
}


?>