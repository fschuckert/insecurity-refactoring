<?php
require_once('singleton.php');

Singleton::getInstance()->setParam($_GET['input']);






echo(Singleton::getInstance()->getParam());

?>