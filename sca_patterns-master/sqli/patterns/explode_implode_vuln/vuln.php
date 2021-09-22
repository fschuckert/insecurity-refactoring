<?php

require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$values = $_GET['values'];

$arr = explode(',', $values);


$sql = "SELECT val FROM Tests WHERE id=" . implode($arr);

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>