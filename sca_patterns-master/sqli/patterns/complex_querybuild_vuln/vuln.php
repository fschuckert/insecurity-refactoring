<?php

require_once("./init.php");
require_once("./sink.php");
require_once("./source.php");

$id = $_GET['id'];

$table = "Tests";
$columns = array( "id" => $id);


$results = findAllByAttributes($table, $columns);

foreach ($results as $row) 
{
    echo $row['val'];
}

?>