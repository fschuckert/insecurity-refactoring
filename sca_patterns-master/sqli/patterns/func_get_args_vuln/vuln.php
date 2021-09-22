<?php

require_once("./init.php");


$id = $_GET['id'];


$results = listWhere($id);

foreach ($results as $row) 
{
    echo $row['val'];
}


function listWhere()
{
    $db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
    $id = func_get_args()[0];
    $sql = "SELECT val FROM Tests WHERE id=$id";
    return $db->query($sql);
}


?>