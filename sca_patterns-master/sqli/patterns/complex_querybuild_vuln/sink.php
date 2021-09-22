<?php

require_once("./CDbCriteria.php");

function query($table, $criteria)
{
    $sql = createFindCommand($table, $criteria);

    $db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

    return $db->query($sql);
}

function findAllByAttributes($table, $columns, $condition='',$params=array())
{

    $criteria=createColumnCriteria($table,$columns,$condition,$params);

    return query($table, $criteria,true);
}

function applyCondition($sql,$condition)
	{
		if($condition!='')
			return $sql.' WHERE '.$condition;
		else
			return $sql;
	}

function createFindCommand($table,$criteria,$alias='t')
	{
        $select=is_array($criteria->select) ? implode(', ',$criteria->select) : $criteria->select;       
       

        $sql='SELECT'." {$select} FROM {$table}";

        $sql=applyCondition($sql,$criteria->condition);
        
		return $sql;
	}

function createCriteria($condition='',$params=array())
	{
		$criteria=new CDbCriteria;
		$criteria->condition=$condition;
		$criteria->params=$params;
		return $criteria;
	}

function createColumnCriteria($table,$columns,$condition='',$params=array(),$prefix=null)
	{
		$criteria= createCriteria($condition,$params);
		// if($criteria->alias!='')
		// 	$prefix=$this->_schema->quoteTableName($criteria->alias).'.';
		$bindByPosition=isset($criteria->params[0]);
		$conditions=array();
		$values=array();
		$i=0;
		foreach($columns as $name=>$value)
		{
            $column = $name;

            if($value!==null)
            {
                if($bindByPosition)
                {
                    $conditions[]=$prefix.$column.'=?';
                    $values[]=$value;
                }
                else
                {
                    $conditions[]=$prefix.$column.'='.$value;
                    $values[$i]=$value;
                    $i++;
                }
            }
            else
                $conditions[]=$prefix.$column->rawName.' IS NULL';

            // print_r($conditions);
		}
		$criteria->params=array_merge($values,$criteria->params);
		if(isset($conditions[0]))
		{
			if($criteria->condition!='')
				$criteria->condition=implode(' AND ',$conditions).' AND ('.$criteria->condition.')';
			else
				$criteria->condition=implode(' AND ',$conditions);
		}
		return $criteria;
	}

?>