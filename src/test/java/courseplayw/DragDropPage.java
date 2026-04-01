package courseplayw;

import com.microsoft.playwright.Page;

public class DragDropPage extends BasePage {
    private DragDropArea dragDropArea;

    public DragDropPage(Page page) {
        super(page);
    }

    /**
     * Ленивая инициализация компонента DragDropArea
     */
    public DragDropArea dragDropArea() {
        if (dragDropArea == null) {
            dragDropArea = new DragDropArea(page);
        }
        return dragDropArea;
    }

    /**
     * Цепочка вызовов для выполнения перетаскивания и проверки результата
     */
    public DragDropPage performDragAndDropAndVerify() {
        dragDropArea().dragAToB();
        return this;
    }

    /**
     * Проверка результата перетаскивания
     */
    public boolean verifyDragResult(String expectedText) {
        return dragDropArea().isTextInBA(expectedText);
    }}