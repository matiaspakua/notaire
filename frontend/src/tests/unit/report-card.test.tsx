/**
 * ReportCard — Wave 6 / #1953
 */
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { FileText } from "lucide-react";
import { ReportCard } from "@/components/shared/ReportCard";

describe("ReportCard (#1953)", () => {
  it("disables download until canDownload is true", async () => {
    const onDownload = vi.fn();
    const { rerender } = render(
      <ReportCard
        testId="card-report-demo"
        icon={FileText}
        title="Budget PDF"
        description="Download one quote"
        downloadLabel="Download PDF"
        downloadTestId="btn-download-demo"
        canDownload={false}
        onDownload={onDownload}
      >
        <span>filters</span>
      </ReportCard>,
    );
    const btn = screen.getByTestId("btn-download-demo");
    expect(btn).toBeDisabled();
    await userEvent.click(btn);
    expect(onDownload).not.toHaveBeenCalled();

    rerender(
      <ReportCard
        testId="card-report-demo"
        icon={FileText}
        title="Budget PDF"
        description="Download one quote"
        downloadLabel="Download PDF"
        downloadTestId="btn-download-demo"
        canDownload
        onDownload={onDownload}
      >
        <span>filters</span>
      </ReportCard>,
    );
    await userEvent.click(screen.getByTestId("btn-download-demo"));
    expect(onDownload).toHaveBeenCalledTimes(1);
  });
});
