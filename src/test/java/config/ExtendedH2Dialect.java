package config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class ExtendedH2Dialect extends H2Dialect {
    public ExtendedH2Dialect() {
        super();
        registerFunction("date", new StandardSQLFunction("date", StandardBasicTypes.DATE));
    }
}
