package com.xforceplus.ultraman.oqsengine.pojo.contract.data;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数据保存对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class EntitySource implements Serializable {
   private IEntity entity;

   public EntitySource() {
   }

   public EntitySource(IEntity entity) {
      this.entity = entity;
   }

   public IEntity getEntity() {
      return entity;
   }

   public void setEntity(IEntity entity) {
      this.entity = entity;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof EntitySource)) return false;
      EntitySource that = (EntitySource) o;
      return Objects.equals(getEntity(), that.getEntity());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getEntity());
   }

}