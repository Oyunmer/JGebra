package net.orisestudios.com;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import net.orisestudios.com.func.Function;
import net.orisestudios.com.func.FunctionParser;
import net.orisestudios.com.func.PolynomialFunction;

public class Renderer extends JPanel {
    private double scale = 50.0;
    private double translateX = 0.0;
    private double translateY = 0.0;
    private Point dragStart;
    private List<Function> functions = new ArrayList<>();
    
    private final Color[] FUNCTION_COLORS = {
        Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.CYAN
    };
    
    private Point lastMousePoint = null;
    private Map<Function, List<Point2D.Double>> functionRoots = new HashMap<>();
    private Map<Function, Point2D.Double> functionYIntercepts = new HashMap<>();
    
    private static final double MIN_SCALE = 0.5;
    private static final double MAX_SCALE = 1000.0;
    
    private static final double MIN_GRID_STEP = 0.1;
    private static final double MAX_GRID_STEP = 100.0;
    private static final int MIN_PIXEL_DISTANCE = 40;
    
    public Renderer() {
        setBackground(new Color(240, 240, 240));
        setupMouseListeners();
    }
    
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
            }
            
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    translateX += dx;
                    translateY -= dy;
                    dragStart = e.getPoint();
                    repaint();
                }
            }
            
            public void mouseMoved(MouseEvent evt) {
                lastMousePoint = evt.getPoint();
                repaint();
            }
        });
        
        addMouseWheelListener(e -> {
            double zoomFactor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            double newScale = scale * zoomFactor;
            
            newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));
            
            if (scale != newScale) {
                Point mousePos = e.getPoint();
                double mouseX = (mousePos.x - getWidth()/2 - translateX) / scale;
                double mouseY = (getHeight()/2 - mousePos.y - translateY) / scale;
                
                scale = newScale;
                
                translateX = mousePos.x - getWidth()/2 - mouseX * scale;
                translateY = getHeight()/2 - mousePos.y - mouseY * scale;
            }
            
            repaint();
        });
    }
    
    public void addFunction(Function f) {
        functions.add(f);
        repaint();
    }
    
    public void removeFunction(int index) {
        if (index >= 0 && index < functions.size()) {
            functions.remove(index);
            repaint();
        }
    }
    
    public void clearFunctions() {
        functions.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawPreciseGrid(g2);
        drawAxes(g2);
        drawFunctions(g2);
        drawIntersectionPoints(g2);
        
        g2.setColor(Color.gray);
        g2.drawString("Mert Ömer Kapçık & Mehmed Kerem Çalışkan ", 000, 658);
        
        if (lastMousePoint != null) {
            drawNearbyPoints(g2, lastMousePoint);
        }
    }
    
    private void drawIntersectionPoints(Graphics2D g2) {
        functionRoots.clear();
        functionYIntercepts.clear();
        
        int width = getWidth();
        int height = getHeight();
        
        for (Function f : functions) {
            double yIntercept = f.evaluate(0);
            Point2D.Double yInterceptPoint = new Point2D.Double(0, yIntercept);
            functionYIntercepts.put(f, yInterceptPoint);
            
            List<Point2D.Double> roots = findRoots(f);
            functionRoots.put(f, roots);
            
            g2.setColor(Color.RED);
            drawPoint(g2, yInterceptPoint);
            
            for (Point2D.Double root : roots) {
                drawPoint(g2, root);
            }
        }
    }

    private List<Point2D.Double> findRoots(Function f) {
        List<Point2D.Double> roots = new ArrayList<>();
        
        if (f instanceof PolynomialFunction) {
            List<FunctionParser.Term> terms = ((PolynomialFunction)f).getTerms();
            
            if (terms.size() == 4) {
                double a = terms.get(0).coefficient;
                double b = terms.get(1).coefficient;
                double c = terms.get(2).coefficient;
                double d = terms.get(3).coefficient;
                
                for (double guess : new double[]{-3, -1, 0, 1, 3}) {
                    double root = findPolynomialRoot(a, b, c, d, guess);
                    if (!Double.isNaN(root) && isUnique(root, roots)) {
                        roots.add(new Point2D.Double(root, 0));
                        if (roots.size() == 3) break;
                    }
                }
                return roots;
            }
            
            if (terms.size() == 3) {
                double a = terms.get(0).coefficient;
                double b = terms.get(1).coefficient;
                double c = terms.get(2).coefficient;
                double discriminant = b*b - 4*a*c;
                
                if (discriminant > 0) {
                    roots.add(new Point2D.Double((-b-Math.sqrt(discriminant))/(2*a), 0));
                    roots.add(new Point2D.Double((-b+Math.sqrt(discriminant))/(2*a), 0));
                } else if (discriminant == 0) {
                    roots.add(new Point2D.Double(-b/(2*a), 0));
                }
                return roots;
            }
        }
        
        double[] searchRanges = {-1000, -2, 0, 2, 1000};
        for (int i = 0; i < searchRanges.length-1; i++) {
            double x1 = searchRanges[i], x2 = searchRanges[i+1];
            double f1 = f.evaluate(x1), f2 = f.evaluate(x2);
            
            if (f1*f2 <= 0) {
                double root = improvedRootFinder(f, x1, x2);
                if (isUnique(root, roots)) {
                    roots.add(new Point2D.Double(root, 0));
                }
            }
        }
        return roots;
    }

    private double findPolynomialRoot(double a, double b, double c, double d, double guess) {
        final double TOLERANCE = 1e-8;
        double x = guess;
        
        for (int i = 0; i < 100; i++) {
            double fx = a*x*x*x + b*x*x + c*x + d;
            double dfx = 3*a*x*x + 2*b*x + c;
            
            if (Math.abs(fx) < TOLERANCE) return x;
            if (Math.abs(dfx) < 1e-12) break;
            
            x = x - fx/dfx;
        }
        return Double.NaN;
    }

    private boolean isUnique(double root, List<Point2D.Double> roots) {
        for (Point2D.Double r : roots) {
            if (Math.abs(root - r.x) < 0.001) return false;
        }
        return true;
    }

    private double improvedRootFinder(Function f, double x1, double x2) {
        final double TOLERANCE = 1e-8;
        final int MAX_ITER = 50;
        
        double f1 = f.evaluate(x1);
        double f2 = f.evaluate(x2);
        
        for (int i = 0; i < MAX_ITER; i++) {
            if (Math.abs(f2 - f1) < TOLERANCE) break;
            
            double x3 = x2 - f2 * (x2 - x1) / (f2 - f1);
            double f3 = f.evaluate(x3);
            
            if (Math.abs(f3) < TOLERANCE) return x3;
            
            x1 = x2;
            x2 = x3;
            f1 = f2;
            f2 = f3;
        }
        
        return binarySearch(f, x1, x2, TOLERANCE);
    }

    private double binarySearch(Function f, double x1, double x2, double tolerance) {
        double f1 = f.evaluate(x1);
        double f2 = f.evaluate(x2);
        
        for (int i = 0; i < 100; i++) {
            double mid = (x1 + x2) / 2;
            double fMid = f.evaluate(mid);
            
            if (Math.abs(fMid) < tolerance || (x2 - x1)/2 < tolerance) {
                return mid;
            }
            
            if (f1 * fMid < 0) {
                x2 = mid;
                f2 = fMid;
            } else {
                x1 = mid;
                f1 = fMid;
            }
        }
        return (x1 + x2) / 2;
    }

    private void drawPoint(Graphics2D g2, Point2D.Double point) {
        int width = getWidth();
        int height = getHeight();
        
        int screenX = (int)(width/2 + (point.x * scale + translateX));
        int screenY = (int)(height/2 - (point.y * scale + translateY));
        
        g2.setColor(Color.RED);
        g2.fill(new Ellipse2D.Double(screenX - 4, screenY - 4, 8, 8));
    }

    private void drawNearbyPoints(Graphics2D g2, Point mousePoint) {
        int width = getWidth();
        int height = getHeight();
        double threshold = 15;
        
        g2.setColor(Color.BLUE);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        
        for (Map.Entry<Function, Point2D.Double> entry : functionYIntercepts.entrySet()) {
            Point2D.Double point = entry.getValue();
            int screenX = (int)(width/2 + (point.x * scale + translateX));
            int screenY = (int)(height/2 - (point.y * scale + translateY));
            
            if (mousePoint.distance(screenX, screenY) < threshold) {
                String label = String.format("(0, %.2f)", point.y);
                g2.drawString(label, screenX + 10, screenY - 10);
            }
        }
        
        for (Map.Entry<Function, List<Point2D.Double>> entry : functionRoots.entrySet()) {
            for (Point2D.Double point : entry.getValue()) {
                int screenX = (int)(width/2 + (point.x * scale + translateX));
                int screenY = (int)(height/2 - (point.y * scale + translateY));
                
                if (mousePoint.distance(screenX, screenY) < threshold) {
                    String label = String.format("(%.2f, 0)", point.x);
                    g2.drawString(label, screenX + 10, screenY - 10);
                }
            }
        }
    }

    private void drawPreciseGrid(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();
        
        double gridStep = calculateOptimalGridStep();
        
        int centerX = (int)(width/2 + translateX);
        int centerY = (int)(height/2 - translateY);
        
        g2.setColor(new Color(230, 230, 230));
        
        for (double x = centerX; x >= 0; x -= gridStep * scale) {
            g2.drawLine((int)x, 0, (int)x, height);
        }
        for (double x = centerX; x <= width; x += gridStep * scale) {
            g2.drawLine((int)x, 0, (int)x, height);
        }
        
        for (double y = centerY; y >= 0; y -= gridStep * scale) {
            g2.drawLine(0, (int)y, width, (int)y);
        }
        for (double y = centerY; y <= height; y += gridStep * scale) {
            g2.drawLine(0, (int)y, width, (int)y);
        }
        
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        
        for (double x = centerX; x >= 0; x -= gridStep * scale) {
            double worldX = (x - width/2 - translateX) / scale;
            drawGridLabel(g2, worldX, (int)x, centerY + 15);
        }
        for (double x = centerX + gridStep * scale; x <= width; x += gridStep * scale) {
            double worldX = (x - width/2 - translateX) / scale;
            drawGridLabel(g2, worldX, (int)x, centerY + 15);
        }
        
        for (double y = centerY; y >= 0; y -= gridStep * scale) {
            double worldY = (height/2 - y - translateY) / scale;
            drawGridLabel(g2, worldY, centerX + 5, (int)y + 5);
        }
        for (double y = centerY + gridStep * scale; y <= height; y += gridStep * scale) {
            double worldY = (height/2 - y - translateY) / scale;
            drawGridLabel(g2, worldY, centerX + 5, (int)y + 5);
        }
    }

    private double calculateOptimalGridStep() {
        double pixelStep = MIN_PIXEL_DISTANCE;
        double worldStep = pixelStep / scale;
        
        double[] possibleSteps = {0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50, 100};
        
        for (double step : possibleSteps) {
            if (step >= worldStep) {
                return step;
            }
        }
        
        return MAX_GRID_STEP;
    }

    private void drawGridLabel(Graphics2D g2, double value, int x, int y) {
        String label;
        if (value == (int)value) {
            label = String.format("%d", (int)value);
        } else {
            if (Math.abs(value) < 0.26) {
                label = String.format("%.2f", value);
            } else if (Math.abs(value) < 1.0) {
                label = String.format("%.1f", value);
            } else {
                label = String.format("%.1f", value);
            }
        }
        g2.drawString(label, x, y);
    }

    private void drawAxes(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();
        
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        
        int xAxisY = (int)(height/2 - translateY);
        g2.drawLine(0, xAxisY, width, xAxisY);
        
        int yAxisX = (int)(width/2 + translateX);
        g2.drawLine(yAxisX, 0, yAxisX, height);
        
        int arrowSize = 8;
        g2.fillPolygon(
            new int[] {width, width - arrowSize, width - arrowSize},
            new int[] {xAxisY, xAxisY - arrowSize/2, xAxisY + arrowSize/2}, 3);
        g2.fillPolygon(
            new int[] {yAxisX, yAxisX - arrowSize/2, yAxisX + arrowSize/2},
            new int[] {0, arrowSize, arrowSize}, 3);
    }

    private void drawFunctions(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();
        
        g2.setStroke(new BasicStroke(2));
        
        for (int i = 0; i < functions.size(); i++) {
            Function f = functions.get(i);
            g2.setColor(FUNCTION_COLORS[i % FUNCTION_COLORS.length]);
            
            Path2D path = new Path2D.Double();
            boolean firstPoint = true;
            
            for (int screenX = 0; screenX < width; screenX++) {
                double worldX = (screenX - width/2 - translateX) / scale;
                double worldY = f.evaluate(worldX);
                int screenY = (int)(height/2 - (worldY * scale + translateY));
                
                if (screenY >= 0 && screenY < height) {
                    if (firstPoint) {
                        path.moveTo(screenX, screenY);
                        firstPoint = false;
                    } else {
                        path.lineTo(screenX, screenY);
                    }
                } else {
                    firstPoint = true;
                }
            }
            
            g2.draw(path);
        }
    }
}