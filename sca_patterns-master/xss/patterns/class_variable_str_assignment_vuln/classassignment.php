<?php

class Foo{
    protected $variable = '';

    public function setParam($variableName, $par){
        $this->$variableName = $par;
    }

    public function getParam(){
        return $this->variable;
    }

}

?>