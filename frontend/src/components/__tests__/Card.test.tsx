import { render, screen } from '@testing-library/react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '../ui/Card';

describe('Card Components', () => {
  test('should render Card with content', () => {
    render(
      <Card>
        <p>Card content</p>
      </Card>
    );
    expect(screen.getByText('Card content')).toBeInTheDocument();
  });

  test('should render Card with header and title', () => {
    render(
      <Card>
        <CardHeader>
          <CardTitle>Card Title</CardTitle>
        </CardHeader>
      </Card>
    );
    expect(screen.getByText('Card Title')).toBeInTheDocument();
  });

  test('should render Card with description', () => {
    render(
      <Card>
        <CardHeader>
          <CardTitle>Title</CardTitle>
          <CardDescription>This is a description</CardDescription>
        </CardHeader>
      </Card>
    );
    expect(screen.getByText('This is a description')).toBeInTheDocument();
  });

  test('should render complete Card with all sections', () => {
    render(
      <Card>
        <CardHeader>
          <CardTitle>Complete Card</CardTitle>
          <CardDescription>With all sections</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Main content here</p>
        </CardContent>
        <CardFooter>
          <p>Footer content</p>
        </CardFooter>
      </Card>
    );
    
    expect(screen.getByText('Complete Card')).toBeInTheDocument();
    expect(screen.getByText('With all sections')).toBeInTheDocument();
    expect(screen.getByText('Main content here')).toBeInTheDocument();
    expect(screen.getByText('Footer content')).toBeInTheDocument();
  });

  test('should render Card with multiple content items', () => {
    render(
      <Card>
        <CardContent>
          <p>Item 1</p>
          <p>Item 2</p>
          <p>Item 3</p>
        </CardContent>
      </Card>
    );
    
    expect(screen.getByText('Item 1')).toBeInTheDocument();
    expect(screen.getByText('Item 2')).toBeInTheDocument();
    expect(screen.getByText('Item 3')).toBeInTheDocument();
  });
});
