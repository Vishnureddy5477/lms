import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PracticeService, CodeRunResult } from '../../../services/practice.service';

interface LanguageOption {
  label: string;
  value: string;
  starter: string;
}

const LANGUAGE_OPTIONS: LanguageOption[] = [
  { label: 'Python', value: 'python', starter: 'print("Hello, world!")\n' },
  { label: 'JavaScript', value: 'javascript', starter: 'console.log("Hello, world!");\n' },
  { label: 'Java', value: 'java', starter: 'public class Main {\n  public static void main(String[] args) {\n    System.out.println("Hello, world!");\n  }\n}\n' },
  { label: 'C', value: 'c', starter: '#include <stdio.h>\n\nint main() {\n  printf("Hello, world!\\n");\n  return 0;\n}\n' },
  { label: 'C++', value: 'c++', starter: '#include <iostream>\n\nint main() {\n  std::cout << "Hello, world!" << std::endl;\n  return 0;\n}\n' },
  { label: 'C#', value: 'csharp', starter: 'using System;\n\nclass Program {\n  static void Main() {\n    Console.WriteLine("Hello, world!");\n  }\n}\n' },
];

@Component({
  selector: 'app-coding-practice',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './coding-practice.component.html',
  styleUrls: ['./coding-practice.component.css']
})
export class CodingPracticeComponent {
  languageOptions = LANGUAGE_OPTIONS;
  selectedLanguage = 'python';
  code = LANGUAGE_OPTIONS[0].starter;
  stdin = '';

  running = false;
  errorMessage = '';
  result: CodeRunResult | null = null;

  constructor(private practiceService: PracticeService) {}

  onLanguageChange(): void {
    const option = this.languageOptions.find((l) => l.value === this.selectedLanguage);
    this.code = option ? option.starter : '';
    this.result = null;
    this.errorMessage = '';
  }

  onRun(): void {
    if (!this.code.trim()) {
      return;
    }
    this.running = true;
    this.errorMessage = '';
    this.result = null;

    this.practiceService
      .runCode({ code: this.code, language: this.selectedLanguage, input: this.stdin })
      .subscribe({
        next: (res) => {
          this.running = false;
          this.result = res;
        },
        error: (err) => {
          this.running = false;
          this.errorMessage = err?.error?.error || err?.error?.message || 'Failed to run code. Please try again.';
        },
      });
  }

  get compileFailed(): boolean {
    return !!this.result?.compile && this.result.compile.code !== 0;
  }
}
