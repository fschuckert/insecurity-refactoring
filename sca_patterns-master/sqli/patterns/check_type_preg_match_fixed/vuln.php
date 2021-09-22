<?php

require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$id = $_GET['id'];

if(preg_match('/^[\.,0-9]+$/i',trim($id)))
{
    $sql = "SELECT val FROM Tests WHERE id=$id";

    $results = $db->query($sql);

    foreach ($results as $row) 
    {
        echo $row['val'];
    }
}
else 
{
    echo "Not an integer value";
}



?>