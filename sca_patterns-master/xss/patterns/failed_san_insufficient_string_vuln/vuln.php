<?php

require_once('./san.php');

if (PMA_isValid($_GET['param'], 'string')) {
    $visualizationSettings = $_GET['param'];
}

echo $visualizationSettings;

?>