package likelion.mcmshowcase.product.entity;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductNameEnMappingTest {

    @Test
    void nameEnMapsToNullableNameEnColumn() throws NoSuchFieldException {
        Field field = Product.class.getDeclaredField("nameEn");
        Column column = field.getAnnotation(Column.class);

        assertEquals("name_en", column.name());
        assertEquals(255, column.length());
        assertTrue(column.nullable());
        assertFalse(column.unique());
    }
}
