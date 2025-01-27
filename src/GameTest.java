// LỚP SỬ DỤNG JUnit ĐỂ KIỂM TRA LỚP GameLogic

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.LinkedList;

import org.junit.Test;

// sử dụng để kiểm tra chức năng của một số tính năng trong trò chơi, 
    // chẳng hạn như kiểm tra xem rắn có đâm vào tường không, trò chơi có kết thúc khi rắn đâm vào thân mình không, hoặc khi rắn ăn thức ăn.
public class GameTest {
    int BOARD_NUM_ROW = 25;
    int BOARD_NUM_COL = 25;
    int OBJECT_SIZE = 20;
    // Kích thước chiều rộng và chiều cao của bàn chơi dựa trên số hàng, cột và kích thước từng ô.
    int COURT_WIDTH = BOARD_NUM_COL * OBJECT_SIZE;
    int COURT_HEIGHT = BOARD_NUM_ROW * OBJECT_SIZE;
    int MIN_SNAKE_LENGTH = 3;
    // hướng đi ban đầu của rắn (phải)
    Direction START_DIRECTION = Direction.RIGHT;

    // kiểm tra xem rắn có đâm vào tường không bằng cách thay đổi hướng của rắn và xác nhận kết quả với phương thức willHitWall()
    @Test
    public void testSnakeWillHitWall() {
        Snake head = new Snake(0, 0, OBJECT_SIZE, COURT_WIDTH, COURT_HEIGHT, Direction.RIGHT, Type.HEAD);

        // Sử dụng assertTrue và assertFalse để xác nhận kết quả kiểm tra.
        assertFalse("Snake will not hit wall", head.willHitWall());

        head.dir = Direction.UP;
        assertTrue("Snake will hit upper wall", head.willHitWall());

        head.dir = Direction.LEFT;
        assertTrue("Snake will hit right wall", head.willHitWall());

        head = new Snake(COURT_WIDTH - 1, COURT_HEIGHT - 1, OBJECT_SIZE, COURT_WIDTH, COURT_HEIGHT, Direction.DOWN, Type.HEAD);
        assertTrue("Snake will hit lower wall", head.willHitWall());

        head.dir = Direction.RIGHT;
        assertTrue("Snake will hit left wall", head.willHitWall());
    }

    // kiểm tra xem trò chơi có kết thúc khi rắn đâm vào tường không
    @Test
    public void testGameWillEndHitWall() {
        boolean playing = true;
        Snake head = new Snake(COURT_WIDTH - 1, COURT_HEIGHT - 1, OBJECT_SIZE, COURT_WIDTH, COURT_HEIGHT, Direction.DOWN, Type.HEAD);
        if (head.willHitWall()) {
            playing = false;
        }
        assertFalse("Game will end", playing);
    }

    // kiểm tra xem trò chơi có kết thúc khi rắn đâm vào thân mình không
    @Test
    public void testGameWillEndEatBody() {
        boolean playing = true;
        Board board = new Board(BOARD_NUM_ROW, BOARD_NUM_COL, OBJECT_SIZE);

        Snake head = new Snake(0, 0, OBJECT_SIZE, COURT_WIDTH, COURT_HEIGHT, Direction.DOWN, Type.HEAD);
        Point next_loc = head.nextLocation();

        // creates a snake body that will be in the head's next location
        Snake body = new Snake(next_loc.x, next_loc.y, OBJECT_SIZE, COURT_WIDTH, COURT_HEIGHT, Direction.DOWN,
                Type.BODY);
        // add the snake body to the board
        board.setObject(body);

        if (!head.willHitWall()) {
            GameObj nextObject = board.getObject(next_loc.x, next_loc.y);
            if (nextObject != null) {
                switch (nextObject.type) {
                case FOOD:
                    break;
                case BODY:
                    playing = false;
                    break;
                case HEAD:
                    break;
                }
            }
        }
        assertFalse("Game will end", playing);
    }

    // kiểm tra xem rắn có di chuyển đúng trên bàn chơi không
    @Test
    public void testMoveSnake() {

        LinkedList<Snake> snake = new LinkedList<Snake>();
        snake.add(new Snake(MIN_SNAKE_LENGTH - 1, 0, OBJECT_SIZE, BOARD_NUM_COL, BOARD_NUM_ROW, Direction.RIGHT,
                Type.HEAD));
        for (int i = MIN_SNAKE_LENGTH - 2; i >= 0; i--) {
            Snake s = new Snake(i, 0, OBJECT_SIZE, BOARD_NUM_COL, BOARD_NUM_ROW, Direction.RIGHT, Type.BODY);
            snake.add(s);
        }

        LinkedList<Snake> snakeToMove = new LinkedList<Snake>();
        snakeToMove.add(new Snake(MIN_SNAKE_LENGTH - 1, 0, OBJECT_SIZE, BOARD_NUM_COL, BOARD_NUM_ROW, Direction.RIGHT,
                Type.HEAD));
        for (int i = MIN_SNAKE_LENGTH - 2; i >= 0; i--) {
            Snake s = new Snake(i, 0, OBJECT_SIZE, BOARD_NUM_COL, BOARD_NUM_ROW, Direction.RIGHT, Type.BODY);
            snakeToMove.add(s);
        }

        Board board = new Board(BOARD_NUM_ROW, BOARD_NUM_COL, OBJECT_SIZE);
        // add snake to board
        board.resetBoard();
        for (int i = snakeToMove.size() - 1; i >= 0; i--) {
            Snake s = snakeToMove.get(i);
            board.setObject(s);
        }

        // moves the objects on the board to the next location
        // directly copied from GameLogic
        for (int i = snakeToMove.size() - 1; i >= 0; i--) {
            Snake s = snakeToMove.get(i);
            s.move();
            snakeToMove.set(i, s);
        }

        // updates the board
        board.resetBoard();
        for (int i = snakeToMove.size() - 1; i >= 0; i--) {
            Snake s = snakeToMove.get(i);
            board.setObject(s);
        }

        boolean moves = false;
        for (int i = 0; i < snake.size(); i++) {
            int x = (int) snake.get(i).nextLocation().getX();
            int y = (int) snake.get(i).nextLocation().getY();
            moves = board.getObject(x, y).equals(snakeToMove.get(i));
        }

        assertTrue("snake moves properly", moves);
    }

    // kiểm tra xem rắn có ăn thức ăn trên bàn chơi không và các hành vi liên quan khi rắn ăn thức ăn
    @Test
    public void testEatFood() {
        boolean ate = false;

        Board board = new Board(BOARD_NUM_ROW, BOARD_NUM_COL, OBJECT_SIZE);

        LinkedList<Snake> snake = new LinkedList<Snake>();
        snake.add(new Snake(MIN_SNAKE_LENGTH - 1, 0, OBJECT_SIZE, BOARD_NUM_COL, BOARD_NUM_ROW, Direction.RIGHT,
                Type.HEAD));
        for (int i = MIN_SNAKE_LENGTH - 2; i >= 0; i--) {
            Snake s = new Snake(i, 0, OBJECT_SIZE, BOARD_NUM_COL, BOARD_NUM_ROW, Direction.RIGHT, Type.BODY);
            snake.add(s);
        }

        Snake head = snake.get(0);
        Point next_loc = head.nextLocation();

        // creates a food that will be in the head's next location
        FoodObj food = new FoodObj(next_loc.x, next_loc.y, OBJECT_SIZE, COURT_WIDTH, COURT_HEIGHT, Direction.DOWN,
                Type.FOOD);

        // creates a temp food to compare with the respawned food later on
        FoodObj temp_food = new FoodObj(next_loc.x, next_loc.y, OBJECT_SIZE, COURT_WIDTH, COURT_HEIGHT, Direction.DOWN,
                Type.FOOD);

        // updates board
        board.resetBoard();
        for (int i = snake.size() - 1; i >= 0; i--) {
            Snake s = snake.get(i);
            board.setObject(s);
        }
        board.setObject(food);

        // this part is almost directly copied from GameLogic
        if (!head.willHitWall()) {
            GameObj nextObject = board.getObject(next_loc.x, next_loc.y);
            if (nextObject != null) {
                switch (nextObject.type) {
                case FOOD:
                    ate = true;
                    Snake lastJoint = snake.get(snake.size() - 1);
                    Snake newJoint = new Snake(lastJoint.pos_x, lastJoint.pos_y, OBJECT_SIZE, BOARD_NUM_COL - 1,
                            BOARD_NUM_ROW - 1, START_DIRECTION, Type.BODY);

                    // moves the Objects
                    for (int i = snake.size() - 1; i >= 0; i--) {
                        Snake s = snake.get(i);
                        s.move();
                        snake.set(i, s);
                    }

                    snake.add(newJoint);

                    // updates the board
                    board.resetBoard();
                    for (int i = snake.size() - 1; i >= 0; i--) {
                        Snake s = snake.get(i);
                        board.setObject(s);
                    }

                    // respawns food: directly copied from GameLogic
                    int numEmptySpaces = BOARD_NUM_ROW * BOARD_NUM_COL - snake.size();
                    int randomIndex = (int) (Math.random() * numEmptySpaces);
                    for (int i = 0; i < BOARD_NUM_COL; i++) {
                        for (int j = 0; j < BOARD_NUM_ROW; j++) {
                            GameObj object = board.getObject(i, j);
                            if (object == null) {
                                randomIndex--;
                                if (randomIndex == -1) {
                                    food.pos_x = i;
                                    food.pos_y = j;
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case BODY:
                    break;
                case HEAD:
                    break;
                }
            }
        }

        // Kiểm tra xem rắn có ăn thức ăn không
        assertTrue("Snake ate the food", ate); 
        // Kiểm tra xem thức ăn có xuất hiện lại sau khi bị ăn không
        assertTrue("Food has respawned", (temp_food.pos_x != food.pos_x) && (temp_food.pos_y != food.pos_y));
        // Kiểm tra xem kích thước của rắn có tăng lên không
        assertTrue("Snake has grown in size", snake.size() == 4);
        // Kiểm tra xem đầu rắn có di chuyển tới vị trí cũ của thức ăn không
        assertTrue("Snake head has moved to old location of food", board.getObject(next_loc.x, next_loc.y).equals(snake.get(0)));
        
    }
}