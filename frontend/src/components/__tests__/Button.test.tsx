import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Button } from '../ui/Button';

describe('Button Component', () => {
  test('should render button with text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByRole('button', { name: /click me/i })).toBeInTheDocument();
  });

  test('should handle click events', async () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Submit</Button>);
    
    const button = screen.getByRole('button', { name: /submit/i });
    await userEvent.click(button);
    
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  test('should be disabled when disabled prop is true', () => {
    render(<Button disabled>Disabled</Button>);
    expect(screen.getByRole('button', { name: /disabled/i })).toBeDisabled();
  });

  test('should apply variant styles correctly', () => {
    const { container } = render(<Button variant="destructive">Delete</Button>);
    const button = screen.getByRole('button', { name: /delete/i });
    expect(button).toHaveClass('destructive');
  });

  test('should support different button types', () => {
    render(<Button type="submit">Submit Form</Button>);
    const button = screen.getByRole('button', { name: /submit form/i });
    expect(button).toHaveAttribute('type', 'submit');
  });
});
