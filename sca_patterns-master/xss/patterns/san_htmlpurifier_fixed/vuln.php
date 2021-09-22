<?php


$tainted = $_GET['tainted'];

require_once 'htmlpurifier/HTMLPurifier.auto.php';
$config = HTMLPurifier_Config::createDefault();
$config->set('HTML.Allowed', 'b,strong,i,em,u,a[href|title],ul,ol,li,p[style],br,span[style]');
$config->set('CSS.AllowedProperties', 'font,font-size,font-weight,font-style,font-family,text-decoration,padding-left,color,background-color,text-align');
$purifier = new HTMLPurifier($config);
$tainted = $purifier->purify($tainted);

echo $tainted;



?>