<?php

if (isset($_POST["OID_AL"]) && isset($_POST["OID_AS"])) {
// 	$oidAs = $_POST["OID_AS"];
	$oidAl = $_POST["OID_AL"];
	$TEST = $oidAs;
	$uno = $TEST;
	$dos = $uno;
	$tres = $dos;
	$cuatro = $tres;

} else {
	if (isset($_SESSION["crearCalificacion"])) {

		$crearCalificacion = $_SESSION["crearCalificacion"];

		$oidAs = $crearCalificacion["OID_AS"];
		$oidAl = $crearCalificacion["OID_AL"];

		unset($_SESSION["crearCalificacion"]);

	} else {
		Header("Location:seleccionarAsignatura.php");
	}
}
$nombreAl = getNombreAlumno($conexion, $oidAl);
$nombreAs = consultarAsignatura($conexion, $oidAs);
foo($TEST);

echo $oidAs; 
echo $oidAl; 
echo $TEST; 
echo $cuatro; 
?>
