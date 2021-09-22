<?php
/**
 * CDbCriteria class file.
 *
 * @author Qiang Xue <qiang.xue@gmail.com>
 * @link http://www.yiiframework.com/
 * @copyright 2008-2013 Yii Software LLC
 * @license http://www.yiiframework.com/license/
 */

/**
 * CDbCriteria represents a query criteria, such as conditions, ordering by, limit/offset.
 *
 * It can be used in AR query methods such as CActiveRecord::find and CActiveRecord::findAll.
 *
 * $criteria=new CDbCriteria();
 * $criteria->compare('status',Post::STATUS_ACTIVE);
 * $criteria->addInCondition('id',array(1,2,3,4,5,6));
 *
 * $posts = Post::model()->findAll($criteria);
 *
 * @author Qiang Xue <qiang.xue@gmail.com>
 * @package system.db.schema
 * @since 1.0
 */
class CDbCriteria
{	
	public $select='*';

	public $condition='';

	public $params=array();
}

?>