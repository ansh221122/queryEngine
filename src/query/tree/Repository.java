package query.tree;

import java.util.List;

public class Repository {

    private Executor executor;
    

    public Repository(Executor executor) {
    	this.executor = executor;
    }


    public <T> List<T> find(Query<T> query) {

    	
          //  return executor.execute(query);
    	return null;
        
    }
}