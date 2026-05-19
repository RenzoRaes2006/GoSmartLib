import { Pipe, PipeTransform } from '@angular/core';
import { Tag, TagType } from './Tag';

@Pipe({ name: 'tagsByType', standalone: true, pure: true })
export class TagsByTypePipe implements PipeTransform {
  transform(tags: Tag[] | null | undefined, type: TagType): Tag[] {
    return (tags ?? []).filter((t) => t.type === type);
  }
}
