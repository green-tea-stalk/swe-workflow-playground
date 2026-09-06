import { Component } from '@angular/core';
import { BulletinBoardComponent } from './components/bulletin-board/bulletin-board.component';

/**
 * Root application component hosting the bulletin board system.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [BulletinBoardComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  /** Application title identifier */
  readonly title = 'bulletin-board';
}
