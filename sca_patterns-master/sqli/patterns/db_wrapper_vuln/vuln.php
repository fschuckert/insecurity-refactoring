<?php

require_once('./init.php');
require_once('./db_wrapper.php');

$id = $_GET['id'];

$sql = "SELECT val FROM Tests WHERE id=$id";

$results = query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}

?>