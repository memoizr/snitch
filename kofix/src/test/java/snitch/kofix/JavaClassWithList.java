package snitch.kofix;

import java.util.List;

public class JavaClassWithList {
   private List<String> theList;

   public JavaClassWithList(List<String> theList) {
      this.theList = theList;
   }

   public List<String> getList() {
      return theList;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      JavaClassWithList that = (JavaClassWithList) o;

      return theList != null ? theList.equals(that.theList) : that.theList == null;
   }

   @Override
   public int hashCode() {
      return theList != null ? theList.hashCode() : 0;
   }
}
