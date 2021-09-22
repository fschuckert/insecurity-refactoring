<?php

require_once('dbwrapper.php');

switch($extension) {
case 'sqlite' :
    include_once 'dbsqlite.php';
    $extension = new SQLite();
    $extension->connect();
    break;
}

$GLOBALS['dbi'] = new DB_Wrapper($extension);

?>