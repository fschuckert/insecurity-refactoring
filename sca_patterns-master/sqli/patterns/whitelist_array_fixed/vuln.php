<?php

require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$structure=array();

$structure['page'][1] = true;
$structure['page'][2] = true;
$structure['page'][3] = true;

$id = $_GET['id'];

if(isset($structure['page'][$id]))
{
    $sql = "SELECT val FROM Tests WHERE id=$id";

    $results = $db->query($sql);

    foreach ($results as $row) 
    {
        echo $row['val'];
    }
}
else {
    echo "Filtered out...";
}


?>