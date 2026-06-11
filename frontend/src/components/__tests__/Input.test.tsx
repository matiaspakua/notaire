import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Input } from '../ui/Input';

describe('Input Component', () => {
  test('should render input field with placeholder', () => {
    render(<Input placeholder="Enter name" />);
    expect(screen.getByPlaceholderText('Enter name')).toBeInTheDocument();
  });

  test('should accept user input', async () => {
    const { container } = render(<Input />);
    const input = container.querySelector('input');
    
    if (input) {
      await userEvent.type(input, 'Test input');
      expect(input).toHaveValue('Test input');
    }
  });

  test('should be disabled when disabled prop is true', () => {
    const { container } = render(<Input disabled />);
    const input = container.querySelector('input');
    expect(input).toBeDisabled();
  });

  test('should handle value change events', async () => {
    const handleChange = jest.fn();
    const { container } = render(<Input onChange={handleChange} />);
    const input = container.querySelector('input') as HTMLInputElement;
    
    if (input) {
      await userEvent.type(input, 'test');
      expect(handleChange).toHaveBeenCalled();
    }
  });

  test('should apply different input types', () => {
    const { container: containerEmail } = render(<Input type="email" />);
    const inputEmail = containerEmail.querySelector('input');
    expect(inputEmail).toHaveAttribute('type', 'email');

    const { container: containerPassword } = render(<Input type="password" />);
    const inputPassword = containerPassword.querySelector('input');
    expect(inputPassword).toHaveAttribute('type', 'password');
  });

  test('should be required when required prop is true', () => {
    const { container } = render(<Input required />);
    const input = container.querySelector('input');
    expect(input).toBeRequired();
  });

  test('should support readonly attribute', () => {
    const { container } = render(<Input readOnly value="Read only" />);
    const input = container.querySelector('input') as HTMLInputElement;
    expect(input).toHaveAttribute('readOnly');
  });
});
